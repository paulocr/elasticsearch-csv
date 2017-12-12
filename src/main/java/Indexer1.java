
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
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
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 *
 * @author paulo
 */
public class Indexer1 {

    public static void main(String[] args) {
        try {
            FileReader inputFile = null;
            File f2 = new File("/Users/paulo/Desktop/A64498F.txt");
            inputFile = new FileReader(f2);
            String indexName = "facebook";
            long time = System.nanoTime();
            IndexResponse response;
            BufferedReader br = new BufferedReader(inputFile);
            String[] data;
            StringBuilder contenido = new StringBuilder();

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
                    .setBulkActions(1000)
                    .setConcurrentRequests(1) // 1 es el default aunque no se indique
                    .build();

            String line;
//            XContentBuilder source = XContentFactory.jsonBuilder().startObject();
            Map<String, Object> source = new HashMap<String, Object>();

            while ((line = br.readLine()) != null) {
                contenido = new StringBuilder();
                if (line.startsWith("NOTICIA") || line.startsWith("COMENTARIO") || line.startsWith("RESPUESTA")) {
                    source.put("tipo", line.trim());
                }

                if (line.startsWith("ID:")) {
                    source.put("id", line.replaceAll("ID:", "").trim());
                }

                if (line.startsWith("CONTENIDO")) {
                    while (!line.contains("FECHA")) {
                        contenido.append(line.replaceAll("CONTENIDO:", "").trim());
                        line = br.readLine();
                    };
                    source.put("contenido", contenido);
                }

                if (line.startsWith("FECHA")) {
                    source.put("fecha", line.replaceAll("FECHA:", "").trim());
                }
                if (line.startsWith("Facebook")) {
                    source.put("facebookId", line.replaceAll("Facebook Id:", "").trim());
                }
                if (line.startsWith("Title")) {
                    source.put("title", line.replaceAll("Title:", "").trim());
                }
                if (line.startsWith("Fuente")) {
                    source.put("fuente", line.replaceAll("Fuente:", "").trim());
                }
                if (line.startsWith("Link")) {
                    source.put("link", line.replaceAll("Link:", "").trim());
                }
                if (line.startsWith("Like")) {
                    source.put("likecount", line.replaceAll("Like Count:", "").trim());

                }
                if (line.startsWith(":")) {
                    bulkProcessor.add(Requests.indexRequest(indexName).type("docs").source(source));
                }
            }
            bulkProcessor.close();
            client.close();
            System.out.println((System.nanoTime() - time) / 1000000000.0 / 60 + " minutos");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Indexer1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Indexer1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Indexer1.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
