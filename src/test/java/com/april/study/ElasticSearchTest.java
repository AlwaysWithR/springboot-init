package com.april.study;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.april.study.demos.features.elastic.ElasticService;
import com.april.study.demos.features.model.IndexProperty;
import com.april.study.demos.features.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private ElasticService elasticService;

    @Test
    public void test() throws IOException {
        // 索引名字
        String indexName = "student";

        // 索引是否存在
        BooleanResponse books = client.indices().exists(e -> e.index(indexName));
        System.out.println("索引是否存在：" + books.value());

        // 创建索引
        CreateIndexResponse res = client.indices().create(c -> c
                .index(indexName)
                .mappings(mappings -> mappings  // 映射
                        .properties("name", p -> p
                                .text(t -> t // text类型，index=false
                                        .index(false)
                                )
                        )
                        .properties("age", p -> p
                                .long_(t -> t) // long类型
                        )
                        .properties("grade", p -> p
                                .text(t -> t.index(false))
                        )
                )
        );

        System.out.println(res);
    }

    @Test
    public void test_create_index() throws IOException {
        Map<String, IndexProperty> mps = new HashMap<>();
        mps.put("id", new IndexProperty("integer", true));
        mps.put("name", new IndexProperty("text", true));
        mps.put("price", new IndexProperty("float", false));

        elasticService.createIndex("product", mps);
    }

    @Test
    public void test_save() throws IOException {
        Product p1 = new Product("1", "MAC AIR Book", 10999.99);
        p1.setTag("Apple");
        Product p2 = new Product("2", "Iphone 18 ProMax", 9999.99);
        p2.setTag("Apple");
        Product p3 = new Product("3", "Mac 小羊皮", 196.99);
        p3.setTag("Makeup");
        elasticService.saveProduct(p3);
    }

    @Test
    public void test_get() throws IOException {
        Product p  = elasticService.getProduct("1");
        System.out.println(p.toString());
    }

    @Test
    public void test_search_list() throws IOException {
        List<Product> res = new ArrayList<>(elasticService.searchProduct("1", "mac"));
        System.out.println(res);
    }
}
