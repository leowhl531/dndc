package dndc.Entity;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;

import java.io.IOException;

import static dndc.Util.Constant.GEO_KEY;

@Data
public class AddressFormatter {


    private String address;
    private String city;
    private String state;
    private String zip;
    private String format;



    public GeoPoint getGeoPoint() throws IOException, InterruptedException, ApiException {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(GEO_KEY)
                .build();
        GeocodingResult[] geocode = GeocodingApi.geocode(context, address + city + state + zip).await();
        System.out.println(geocode[0]);
        this.format = geocode[0].formattedAddress;
        return new GeoPoint(geocode[0].geometry.location.lat, geocode[0].geometry.location.lng);
    }
}
