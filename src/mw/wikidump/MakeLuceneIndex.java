/* Copyright 2011 Manuel Wahle
 *
 * This file is part of Hello-Wiki.
 *
 *    Hello-Wiki is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Hello-Wiki is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Hello-Wiki.  If not, see <http://www.gnu.org/licenses/>.
 */

package mw.wikidump;


import java.io.File;
import java.io.IOException;

import mw.utils.NanoTimeFormatter;
import mw.utils.PlainLogger;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

/**
 * @author mwahle
 * 
 */
public class MakeLuceneIndex
{

    /**
     * @param args
     * @throws IOException
     * @throws ParseException
     */
    public static void main(String[] args) throws IOException, ParseException
    {
        String baseDir = "";
        String wikiDumpFile = "enwiki-20110405-pages-articles.xml";
        String luceneIndexName = "enwiki-20110405-lucene";
        String logFile = luceneIndexName + ".log";
        boolean bIgnoreStubs = false;

        for ( int i = 0; i < args.length; ++i )
        {
            if ( args[i].equals( "-luceneindex" ) )
                luceneIndexName = args[++i];

            if ( args[i].equals( "-basedir" ) )
                baseDir = args[++i];

            if ( args[i].equals( "-logfile" ) )
                logFile = args[++i];

            if ( args[i].equals( "-dumpfile" ) )
                wikiDumpFile = args[++i];

            if ( args[i].equals( "-ignorestubs" ) )
                bIgnoreStubs = true;
        }


        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper( new WhitespaceAnalyzer( ) );
        analyzer.addAnalyzer( "tokenized_title", new StandardAnalyzer( Version.LUCENE_30 ) );
        analyzer.addAnalyzer( "contents", new StandardAnalyzer( Version.LUCENE_30 ) );


        File basePath = new File( baseDir );
        File luceneIndex = new File( basePath.getCanonicalPath() + File.separator + luceneIndexName );

        logFile = basePath.getCanonicalPath() + File.separator + logFile;

        // log to file and console:
        // PlainLogger logger = new PlainLogger( logFile );
        // log only to console:
        PlainLogger logger = new PlainLogger();

        logger.log( "Work directory:     " + basePath.getCanonicalPath() );
        logger.log( "Lucene index:       " + luceneIndexName );
        logger.log( "Wikipedia dumpfile: " + wikiDumpFile );
        logger.log( "" );
        if ( bIgnoreStubs )
            logger.log( "Ignoring stubs" );
        else
            logger.log( "Including stubs" );
        logger.log( "" );


        // create the index
        Directory indexDirectory = new MMapDirectory( luceneIndex, org.apache.lucene.store.NoLockFactory.getNoLockFactory() );
        IndexWriter indexWriter = new IndexWriter( indexDirectory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED );


        Extractor wikidumpExtractor = new Extractor( basePath.getCanonicalPath() + File.separator + wikiDumpFile );
        wikidumpExtractor.setLinkSeparator( "_" );
        wikidumpExtractor.setCategorySeparator( "_" );

        int iStubs = 0;
        int iArticleCount = 0;
        int iSkippedPageCount = 0;
        long iStartTime = java.lang.System.nanoTime();
        long iTime = iStartTime;

        while ( wikidumpExtractor.nextPage() )
        {
            if ( wikidumpExtractor.getPageType() != Extractor.PageType.ARTICLE )
            {
                ++iSkippedPageCount;
                continue;
            }

            if ( bIgnoreStubs && wikidumpExtractor.getStub() )
            {
                ++iStubs;
                continue;
            }

            Document doc = new Document();
            ++iArticleCount;


            wikidumpExtractor.setTitleSeparator( "_" );
            doc.add( new Field( "title", wikidumpExtractor.getPageTitle( false ).toLowerCase(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES ) );

            wikidumpExtractor.setTitleSeparator( " " );
            doc.add( new Field( "tokenized_title", wikidumpExtractor.getPageTitle( false ).toLowerCase(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES ) );

            doc.add( new Field( "categories", wikidumpExtractor.getPageCategories().toLowerCase(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES ) );
            doc.add( new Field( "links", wikidumpExtractor.getPageLinks().toLowerCase(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES ) );
            doc.add( new Field( "contents", wikidumpExtractor.getPageAbstract().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );


            indexWriter.addDocument( doc );

            if ( iArticleCount % 50000 == 0 )
            {
                logger.add( iArticleCount + " (" + NanoTimeFormatter.getS( System.nanoTime() - iTime ) + "s) " );
                iTime = System.nanoTime();

                if ( iArticleCount % 250000 == 0 )
                {
                    try
                    {
                        indexWriter.commit();
                        logger.add( "-- commit. Skipped page count " + iSkippedPageCount + " (+ " + iStubs + " stubs)" );
                        logger.log( String.format( ", time %sm", NanoTimeFormatter.getM( System.nanoTime() - iStartTime ) ) );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        logger.log( "" );
        logger.log( String.format( "Overall time %s minutes, ", NanoTimeFormatter.getM( System.nanoTime() - iStartTime ) ) );
        logger.add( "collected " + iArticleCount + " articles, " );
        logger.add( "skipped " + iSkippedPageCount + " nonarticle pages," );
        logger.log( "skipped " + iStubs + " stubs." );
        logger.log( "" );

        iTime = System.nanoTime();
        logger.add( "Optimizing... " );
        indexWriter.optimize();
        logger.add( "done in " + NanoTimeFormatter.getS( System.nanoTime() - iTime ) + "s," );

        iTime = System.nanoTime();
        logger.add( " closing..." );
        indexWriter.close();
        logger.log( " done in " + NanoTimeFormatter.getS( System.nanoTime() - iTime ) + "s." );

        logger.close();
        System.exit( 0 );
    }
}
