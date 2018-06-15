package no.chess.web.server.resource;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

public class MyApplication extends Application {
    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 8182);
        c.getClients().add(Protocol.CLAP);
        c.getDefaultHost().attach(new MyApplication());
        c.start();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        Directory directory = new Directory(getContext(),
                LocalReference.createClapReference(LocalReference.CLAP_CLASS,
                        "/stat"));
        directory.setIndexName("index.html");
        Directory cssDirectory = new Directory(getContext(),
                LocalReference.createClapReference(LocalReference.CLAP_CLASS,
                        "/css"));
        cssDirectory.setIndexName("index.html");
        router.attach("/stat/", directory);
//        router.attach("/css/",cssDirectory);
        return router;
    }
}
