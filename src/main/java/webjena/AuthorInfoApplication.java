package webjena;

import javafx.util.Pair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rdf.AuthorInfo;

/**
 * Created by jachinma on 6/7/17.
 */

@RestController
public class AuthorInfoApplication {

    private String authorDemo =
//            "<!DOCTYPE html>\n"+
            "<html>\n"+
            "  <head>  \n"+
            "        <meta charset=\"utf-8\"/>\n"+
            "        <title>[[${name}]]</title>  \n"+
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
            "        <div id=\"div-comment\">trying to get comment</div>\n"+
            "        <script src=\"d3.v3.min.js\" charset=\"utf-8\"></script>\n"+
            "        <script src=\"jquery-3.2.1.min.js\" charset=\"utf-8\"></script>\n"+
            "        <script>\n"+
            "                       \n"+
            "        var nodes = [[${nodes}]];\n"+
            "        var edges = [[${edges}]];\n"+
            "        \n"+
            "        var width = window.innerWidth;\n"+
            "        var height = window.innerHeight;\n"+
            "        \n"+
            "        \n"+
            "        var svg = d3.select(\"body\")\n"+
            "                    .append(\"svg\")\n"+
            "                    .attr(\"width\",width)\n"+
            "                    .attr(\"height\",height);\n"+
            "        \n"+
            "        var force = d3.layout.force()\n"+
            "                .nodes(nodes)        //指定节点数组\n"+
            "                .links(edges)        //指定连线数组\n"+
            "                .size([width,height])    //指定范围\n"+
            "                .linkDistance(250)    //指定连线长度\n"+
            "                .charge(-400);    //相互之间的作用力\n"+
            "\n"+
            "        force.start();    //开始作用\n"+
            "\n"+
            "        console.log(nodes);\n"+
            "        console.log(edges);\n"+
            "\n"+
            "        var tip = d3.select(\"body\").append(\"div\")\n"+
            "                  .attr(\"class\", \"Mytip\")\n"+
            "                  .attr(\"opacity\", 0)\n"+
            "                  .attr(\"stroke-opacity\", 0);\n"+
            "        \n"+
            "        //添加连线        \n"+
            "        var svg_edges = svg.selectAll(\"line\")\n"+
            "                            .data(edges)\n"+
            "                            .enter()\n"+
            "                            .append(\"line\")\n"+
            "                            .style(\"stroke\",\"#ccc\")\n"+
            "                            .style(\"stroke-width\", function(d) {\n"+
            "                                return d.value;\n"+
            "                            })\n"+
            "                            .on(\"mouseover\", function (d) {\n"+
            "                                tip.html(d.value + \" times\")\n"+
            "                                    tip.style(\"left\", (d.source.x + d.target.x) / 2 +11)\n"+
            "                                    tip.style(\"top\", (d.source.y + d.target.y) / 2 + 11)\n"+
            "                                    tip.style(\"opacity\", 1.0);console.log(d.source.x + d.target.x)\n"+
            "                            })\n"+
            "                            .on(\"mousemove\", function (d) {\n"+
            "                                tip.style(\"left\", (d.source.x + d.target.x) / 2 +11)\n"+
            "                                    tip.style(\"top\", (d.source.y + d.target.y) / 2 + 11)\n"+
            "                                    tip.style(\"opacity\", 1.0);console.log(d.source.x + d.target.x)\n"+
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
            "        var color = d3.scale.category20();\n"+
            "                \n"+
            "        //添加节点            \n"+
            "        var svg_nodes = svg.selectAll(\"circle\")\n"+
            "                            .data(nodes)\n"+
            "                            .enter()\n"+
            "                            .append(\"circle\")\n"+
            "                            .attr(\"r\",20)\n"+
            "                            .style(\"fill\",function(d,i){\n"+
            "                                return color(i);\n"+
            "                            })\n"+
            "                            .on(\"mouseover\", function (d) {\n"+
            "                                 highlightNode(d.index);\n"+
            "                             })\n"+
            "                            .on(\"click\", function (d) {\n" +
            "                                jumpToAuthor(d.name);\n" +
            "                            })"+
            "                            .call(force.drag);\n"+
            "\n"+
            "        //添加描述节点的文字\n"+
            "        var svg_texts = svg.selectAll(\"text\")\n"+
            "                            .data(nodes)\n"+
            "                            .enter()\n"+
            "                            .append(\"text\")\n"+
            "                            .style(\"fill\", \"black\")\n"+
            "                            .attr(\"dx\", 20)\n"+
            "                            .attr(\"dy\", 8)\n"+
            "                            .text(function(d){\n"+
            "                                return d.name;\n"+
            "                            });\n"+
            "                    \n"+
            "\n"+
            "        force.on(\"tick\", function(){    //对于每一个时间间隔\n"+
            "        \n"+
            "             //更新连线坐标\n"+
            "             svg_edges.attr(\"x1\",function(d){ return d.source.x; })\n"+
            "                     .attr(\"y1\",function(d){ return d.source.y; })\n"+
            "                     .attr(\"x2\",function(d){ return d.target.x; })\n"+
            "                     .attr(\"y2\",function(d){ return d.target.y; });\n"+
            "             \n"+
            "             //更新节点坐标\n"+
            "             svg_nodes.attr(\"cx\",function(d){ return d.x; })\n"+
            "                     .attr(\"cy\",function(d){ return d.y; });\n"+
            "\n"+
            "             //更新文字坐标\n"+
            "             svg_texts.attr(\"x\", function(d){ return d.x; })\n"+
            "                 .attr(\"y\", function(d){ return d.y; });\n"+
            "        });\n"+
            "\n"+
            "        function highlightNode(node) {\n"+
            "            svg_edges.each(function (d) {\n"+
            "                this.style.stroke = \"#ccc\";\n"+
            "                this.style.opacity = \"0.5\";\n"+
            "                if (d.source.index == node || d.target.index == node) {\n"+
            "                    this.style.stroke = \"red\";\n"+
            "                    this.style.opacity = \"0.9\";\n"+
            "                }\n"+
            "            });\n"+
            "        }\n"+
            "        function jumpToAuthor(name) {\n" +
            "            window.location.href = \"http://localhost:8080/author?q=\"+name;\n" +
            "        }"+
            "\n"+
            "          \n"+
            "        </script>  \n"+
            "        <script type=\"text/javascript\">\n" +
            "            $.get({url:\"http://localhost:8080/author/[[${name}]]\",success:function(result){\n" +
            "                $(\"#div-comment\").html(result);\n" +
            "            }});\n" +
            "        </script>"+
            "        \n"+
            "    </body>  \n"+
            "</html>  \n";

    @RequestMapping("/author")
    public String author(@RequestParam(value="q") String q) {
        Pair<String, String> p = AuthorInfo.getAuthorRelatedJson(q);
        String outHTML = authorDemo.replace("[[${nodes}]]", p.getKey());
        outHTML = outHTML.replace("[[${edges}]]", p.getValue());
        outHTML = outHTML.replace("[[${name}]]", q);
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
