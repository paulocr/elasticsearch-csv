import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 *
 * @author paulo
 */
public class Indexer {


    public static void main(String[] args) throws FileNotFoundException {
        
        String indexName = args[0];
        FileReader inputFile = new FileReader(new File(args[1]));           

        try {
            long time = System.nanoTime();
            CSVReader cr = new CSVReader(inputFile);
            IndexResponse response;
            String[] data;
            
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticCluster")
                    .build();

            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

//              delete indexName if exists
//            if (client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
//                client.admin().indices().prepareDelete(indexName).execute().actionGet();
//            }
//              create index
//            client.admin().indices().prepareCreate(indexName).execute().actionGet();
            client.admin().cluster().prepareHealth(indexName).setWaitForYellowStatus().execute().actionGet();
            BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    System.out.println("Se indexó " + request.requests().size() + " documentos en " + response.getTook());
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    System.out.println(failure);
                }
                /* Listener methods */ })
                    .setBulkActions(10000)
                    .setConcurrentRequests(1) // 1 es el default aunque no se indique
                    .build();

            String[] fieldNames = cr.readNext();
            String[] headers = new String[fieldNames.length];

            for (int i = 0; i < fieldNames.length; i++) {
                headers[i] = fieldNames[i];
//                System.out.println(headers[i]); // imprimir headers
            }

            while ((data = cr.readNext()) != null) {
                XContentBuilder source = XContentFactory.jsonBuilder().startObject();
                for (int i = 0; i < data.length; i++) {
                    source.field(headers[i], data[i]);
                }
                source.endObject();
                bulkProcessor.add(Requests.indexRequest(indexName).type("docs").source(source));
            }

            bulkProcessor.close();
            client.close();
            System.out.println((System.nanoTime() - time) / 1000000000.0 / 60 + " minutos");
        } catch (Exception ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex.getCause());
        }
    }
}
