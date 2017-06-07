package webjena;

import javafx.util.Pair;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rdf.AuthorInfo;
import rdf.CoauthorInfo;

import javax.websocket.server.PathParam;

/**
 * Created by jachinma on 6/7/17.
 */

@RestController
public class AuthorInfoApplication {

    private String authorDemo = "<html xmlns:th=\"http://www.thymeleaf.org\">\n"+
            "  <head>  \n"+
            "        <meta charset=\"utf-8\"/>\n"+
            "        <title>coauthor</title>  \n"+
            "  </head> \n"+
            "      <style>\n"+
            "    .MyTip {\n"+
            "    position: absolute;\n"+
            "    width: auto;\n"+
            "    height: auto;\n"+
            "    font-family: Verdana;\n"+
            "    font-size: 18px;\n"+
            "    text-align: center;\n"+
            "    border-style: solid;\n"+
            "    border-width: 0.4px;\n"+
            "    background-color: white;\n"+
            "    border-radius: 0.5px;\n"+
            "    }\n"+
            "    </style>\n"+
            "\n"+
            "<style>\n"+
            "\n"+
            "\n"+
            "</style>\n"+
            "    <body>  \n"+
            "\t\t<script src=\"/d3.v3.min.js\" charset=\"utf-8\"></script>\n"+
            "        <script th:inline=\"javascript\">\n"+
            "\t\t\t\t\t   \n"+
            "        var nodes = [[${nodes}]];\n"+
            "        var edges = [[${edges}]];\n"+
            "\t\t\n"+
            "\t\tvar width = 720;\n"+
            "\t\tvar height = 720;\n"+
            "\t\t\n"+
            "\t\t\n"+
            "\t\tvar svg = d3.select(\"body\")\n"+
            "\t\t\t\t\t.append(\"svg\")\n"+
            "\t\t\t\t\t.attr(\"width\",width)\n"+
            "\t\t\t\t\t.attr(\"height\",height);\n"+
            "\t\t\n"+
            "\t\tvar force = d3.layout.force()\n"+
            "\t\t\t\t.nodes(nodes)\t\t//指定节点数组\n"+
            "\t\t\t\t.links(edges)\t\t//指定连线数组\n"+
            "\t\t\t\t.size([width,height])\t//指定范围\n"+
            "\t\t\t\t.linkDistance(150)\t//指定连线长度\n"+
            "\t\t\t\t.charge(-400);\t//相互之间的作用力\n"+
            "\n"+
            "\t\tforce.start();\t//开始作用\n"+
            "\n"+
            "\t\tconsole.log(nodes);\n"+
            "\t\tconsole.log(edges);\n"+
            "\n"+
            "        var tip = d3.select(\"body\").append(\"div\")\n"+
            "                  .attr(\"class\", \"Mytip\")\n"+
            "                  .attr(\"opacity\", 0)\n"+
            "                  .attr(\"stroke-opacity\", 0);\n"+
            "\t\t\n"+
            "\t\t//添加连线\t\t\n"+
            "\t\tvar svg_edges = svg.selectAll(\"line\")\n"+
            "\t\t\t\t\t\t\t.data(edges)\n"+
            "\t\t\t\t\t\t\t.enter()\n"+
            "\t\t\t\t\t\t\t.append(\"line\")\n"+
            "\t\t\t\t\t\t\t.style(\"stroke\",\"#ccc\")\n"+
            "\t\t\t\t\t\t\t.style(\"stroke-width\", function(d) {\n"+
            "\t\t\t\t\t\t\t\treturn d.value;\n"+
            "\t\t\t\t\t\t\t})\n"+
            "                            .on(\"mouseover\", function (d) {\n"+
            "                                tip.html(d.value + \" times\")\n"+
            "                                    .style(\"left\", (d.source.x + d.target.x) / 2 +11)\n"+
            "                                    .style(\"top\", (d.source.y + d.target.y) / 2 + 11)\n"+
            "                                    .style(\"opacity\", 1.0);\n"+
            "                            })\n"+
            "                            .on(\"mousemove\", function (d) {\n"+
            "                                tip.style(\"left\", (d.source.x + d.target.x) / 2 +11)\n"+
            "                                    .style(\"top\", (d.source.y + d.target.y) / 2 + 11)\n"+
            "                                    .style(\"opacity\", 1.0);\n"+
            "                            })\n"+
            "                            .on(\"mouseout\", function (d) {\n"+
            "                                tip.transition()\n"+
            "                                    .duration(1000).style(\"opacity\", 0.0);\n"+
            "                                window.setTimeout(function () {\n"+
            "                                    tip.style(\"left\", 0)\n"+
            "                                        .style(\"top\", 0)\n"+
            "                                }, 1000)\n"+
            "                            });\n"+
            "\n"+
            "\t\tvar color = d3.scale.category20();\n"+
            "\t\t\t\t\n"+
            "\t\t//添加节点\t\t\t\n"+
            "\t\tvar svg_nodes = svg.selectAll(\"circle\")\n"+
            "\t\t\t\t\t\t\t.data(nodes)\n"+
            "\t\t\t\t\t\t\t.enter()\n"+
            "\t\t\t\t\t\t\t.append(\"circle\")\n"+
            "\t\t\t\t\t\t\t.attr(\"r\",20)\n"+
            "\t\t\t\t\t\t\t.style(\"fill\",function(d,i){\n"+
            "\t\t\t\t\t\t\t\treturn color(i);\n"+
            "\t\t\t\t\t\t\t})\n"+
            "\t\t\t\t\t\t\t.on(\"mouseover\", function (d) {\n"+
            "\t\t\t                 \thighlightNode(d.index);\n"+
            "\t\t\t                 })\n"+
            "\t\t\t\t\t\t\t.call(force.drag);\n"+
            "\n"+
            "\t\t//添加描述节点的文字\n"+
            "\t\tvar svg_texts = svg.selectAll(\"text\")\n"+
            "\t\t\t\t\t\t\t.data(nodes)\n"+
            "\t\t\t\t\t\t\t.enter()\n"+
            "\t\t\t\t\t\t\t.append(\"text\")\n"+
            "\t\t\t\t\t\t\t.style(\"fill\", \"black\")\n"+
            "\t\t\t\t\t\t\t.attr(\"dx\", 20)\n"+
            "\t\t\t\t\t\t\t.attr(\"dy\", 8)\n"+
            "\t\t\t\t\t\t\t.text(function(d){\n"+
            "\t\t\t\t\t\t\t\treturn d.name;\n"+
            "\t\t\t\t\t\t\t});\n"+
            "\t\t\t\t\t\n"+
            "\n"+
            "\t\tforce.on(\"tick\", function(){\t//对于每一个时间间隔\n"+
            "\t\t\n"+
            "\t\t\t //更新连线坐标\n"+
            "\t\t\t svg_edges.attr(\"x1\",function(d){ return d.source.x; })\n"+
            "\t\t\t \t\t.attr(\"y1\",function(d){ return d.source.y; })\n"+
            "\t\t\t \t\t.attr(\"x2\",function(d){ return d.target.x; })\n"+
            "\t\t\t \t\t.attr(\"y2\",function(d){ return d.target.y; });\n"+
            "\t\t\t \n"+
            "\t\t\t //更新节点坐标\n"+
            "\t\t\t svg_nodes.attr(\"cx\",function(d){ return d.x; })\n"+
            "\t\t\t \t\t.attr(\"cy\",function(d){ return d.y; });\n"+
            "\n"+
            "\t\t\t //更新文字坐标\n"+
            "\t\t\t svg_texts.attr(\"x\", function(d){ return d.x; })\n"+
            "\t\t\t \t.attr(\"y\", function(d){ return d.y; });\n"+
            "\t\t});\n"+
            "\n"+
            "\t    function highlightNode(node) {\n"+
            "            svg_edges.each(function (d) {\n"+
            "                this.style.stroke = \"#ccc\";\n"+
            "                this.style.opacity = \"0.5\";\n"+
            "                if (d.source.index == node || d.target.index == node) {\n"+
            "                    this.style.stroke = \"red\";\n"+
            "                    this.style.opacity = \"0.9\";\n"+
            "                }\n"+
            "            });\n"+
            "        }\n"+
            "\n"+
            "\t\t  \n"+
            "        </script>  \n"+
            "\t\t\n"+
            "    </body>  \n"+
            "</html>  \n";

    @RequestMapping("/author")
    public String author(@RequestParam(value="q") String q) {
        Pair<String, String> p = CoauthorInfo.getCoauthorJsonByName(q);
        String outHTML = authorDemo.replace("[[${nodes}]]", p.getKey());
        outHTML = outHTML.replace("[[${edges}]]", p.getValue());
        return outHTML;
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
