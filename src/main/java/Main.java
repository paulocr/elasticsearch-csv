

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import org.elasticsearch.action.search.SearchResponse;

public class Main {

    public static void main(String[] args) {
        try {
            String indexName = "indexName";
            File f = new File("[pathToTheCsvFile");
            FileReader inputFile = new FileReader(f);
			Indexer.index(indexName, inputFile);
        } catch (FileNotFoundException ex) {
            System.err.println("File not found");
        }
    }

}
