package xmlManager;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.FileReader;
import java.net.URI;

public class Resolver implements EntityResolver {
    public InputSource resolveEntity(String publicID, String systemID){
        try{
            URI uri = new URI(systemID);
            if("file".equals(uri.getScheme())){
                String fileName = uri.getSchemeSpecificPart();
                return new InputSource(new FileReader(fileName));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
