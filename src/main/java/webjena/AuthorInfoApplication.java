package webjena;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rdf.AuthorInfo;

import javax.websocket.server.PathParam;

/**
 * Created by jachinma on 6/7/17.
 */

@RestController
public class AuthorInfoApplication {

    @RequestMapping("/author")
    public String author(@RequestParam(value="q") String q) {
        return AuthorInfo.getAuthorInfoFromDBPedia(q).replace("@en","");
    }

    @RequestMapping("author/{name}")
    public String authorInfo(@PathVariable("name") String name) {
        return AuthorInfo.getAuthorInfoFromDBPedia(name).replace("@en","");
    }

    @RequestMapping("/")
    public String index() {
        return "<script language=\"javascript\">\n" +
                "    document.location = \"http://localhost:8080/index.html\";\n" +
                "</script>";
    }

    @RequestMapping("/all")
    public String all() {
        return "all";
    }

}
