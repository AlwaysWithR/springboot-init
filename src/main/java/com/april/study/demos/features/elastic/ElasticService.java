package com.april.study.demos.features.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.json.JsonData;
import com.april.study.demos.features.model.IndexProperty;
import com.april.study.demos.features.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticService {

    @Autowired
    private ElasticsearchClient esClient;

    private final String indexName = "product"; // Elasticsearch 索引名称

    public void createIndex(String indexName, Map<String, IndexProperty> mappings) throws IOException {
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, IndexProperty> entry : mappings.entrySet()) {
            String fieldType = entry.getValue().getProperty();
            Boolean index = entry.getValue().getIndex();
            Property property = switch (fieldType.toLowerCase()) {
                case "text" -> Property.of(p -> p.text(t->t.index(index)));
                case "keyword" -> Property.of(p -> p.keyword(t->t.index(index)));
                case "integer" -> Property.of(p -> p.integer(t->t.index(index)));
                case "float" -> Property.of(p -> p.float_(t->t.index(index)));
                case "boolean" -> Property.of(p -> p.boolean_(t->t.index(index)));
                // 添加其他类型的处理
                default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
            };

            // 根据字段类型选择合适的 Property

            properties.put(entry.getKey(), property);
        }

        // 创建索引请求
        CreateIndexRequest request = CreateIndexRequest.of(c -> c
                .index(indexName)
                .mappings(m -> m
                        .properties(properties)
                )
                .settings(s -> s
                        .numberOfShards(String.valueOf(1)) // 设置分片数
                        .numberOfReplicas(String.valueOf(0)) // 设置副本数
                )
        );

        // 执行创建索引请求
        esClient.indices().create(request);
    }

    // 创建或更新文档
    public void saveProduct(Product product) throws IOException {
        IndexRequest<Product> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(product.getId())
                .document(product)
        );
        System.out.println("es save response" + esClient.index(request));
    }

    public Product getProduct(String id) throws IOException {
        GetRequest getRequest = GetRequest.of(g->
                    g.index(indexName)
                            .id(id)
                );

        GetResponse<Product> response = esClient.get(getRequest, Product.class);
        return response.found() ? response.source(): null;
    }

    public List<Product> searchProduct(String id, String name) throws IOException {
        SearchRequest request = SearchRequest.of( s ->
                s.index(indexName)
                        .sort(sort -> sort.field(field -> field.field("id").order(SortOrder.Desc)))
                        // 查询全部就不写query
                        .query( q ->
//                                q.term(t->t.field("tag").value("Apple"))  //term用于精准匹配类型为keyword的字段，不对参数分词，区分大小写
                             q.bool(b -> b
                                     .must( sh -> sh.match(  ma -> ma.field("name").query(name) ))
                                     .must( ms -> ms.term( te->te.field("tag").value("Makeup")))
                                     .filter(f -> f.range(r ->
                                             r.field("price").lte(JsonData.of(100000.00)).gte(JsonData.of(1.00)))
                                     )
                                     .should(sh -> sh.term( t -> t.field("name").value("MAC AIR Book") ))
                             )
                        ) // 分页, 也是从0开始
//                        .from(0).size(1)
        );

        SearchResponse<Product> response = esClient.search(request, Product.class);

        return response.hits().hits().stream().map(Hit::source).toList();
    }
}
