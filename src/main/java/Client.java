
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class Client {

    private static long totalHits;
    private static SearchResponse response;

    public static long returnHits(String query) {

        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticCluster")
                    .build();

            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

            totalHits = client.prepareSearch()
                    .setQuery(QueryBuilders.queryStringQuery(query))
                    .get()
                    .getHits()
                    .getTotalHits();

        } catch (UnknownHostException ex) {
        }

        return totalHits;

    }

    public static void main (String[] args) {
        String query = args[0];
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticCluster")
                    .build();

            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

            response = client.prepareSearch()
                    .setQuery(QueryBuilders.queryStringQuery(query))
                    .get();

        } catch (UnknownHostException ex) {
        }

        System.out.println(response.toString());

    }

}
