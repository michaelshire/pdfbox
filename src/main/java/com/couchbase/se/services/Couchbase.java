package com.couchbase.se.services;

import com.couchbase.client.core.deps.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.core.error.BucketExistsException;
import com.couchbase.client.core.error.CollectionExistsException;
import com.couchbase.client.core.error.ScopeExistsException;
import com.couchbase.client.java.*;
import com.couchbase.client.java.codec.RawJsonTranscoder;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.MutationState;
import com.couchbase.client.java.kv.UpsertOptions;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.collection.CollectionManager;
import com.couchbase.client.java.manager.collection.CollectionSpec;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.search.HighlightStyle;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.result.SearchResult;
import com.google.gson.GsonBuilder;
import com.couchbase.client.java.env.ClusterEnvironment;

import java.time.Duration;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.couchbase.client.java.search.SearchOptions.searchOptions;

public class Couchbase {
    private Cluster cluster;
    private Bucket bucket;
    private Scope scope;
    private Collection collection;

    public Couchbase() {
        // Set Log Level First
        Logger logger = Logger.getLogger("com.couchbase.client");
        logger.setLevel(Level.SEVERE);
        for (Handler h: logger.getParent().getHandlers()) {
            if (h instanceof ConsoleHandler) {
                h.setLevel(Level.SEVERE);
            }
        }
        String hostname = "127.0.0.1";
        String username = "Administrator";
        String password = "password";
        hostname = System.getenv("PDFBOX_HOSTNAME");
        username = System.getenv("PDFBOX_USERNAME");
        password = System.getenv("PDFBOX_PASSWORD");
        ClusterEnvironment env = ClusterEnvironment.builder()
                .securityConfig(SecurityConfig.enableTls(true)
                        .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE))
                .ioConfig(IoConfig.enableDnsSrv(true))
                .build();
        this.cluster = Cluster.connect(hostname,
                ClusterOptions.clusterOptions(username, password).environment(env));
    }

    public void getBucket() {
        this.bucket = cluster.bucket("pdf");
        this.bucket.waitUntilReady(Duration.parse("PT10S"));
        this.scope = bucket.scope("pdf");
        this.collection = scope.collection("pdf");
    }

    public void disconnect() {
        this.cluster.disconnect();
    }

    public void exec_n1ql(String n1ql) {
        QueryResult result = cluster.query(n1ql);
    }

    public SearchResult fts_search(String searchTerm) {
        final SearchResult result = this.cluster.searchQuery("pdf", SearchQuery.queryString(searchTerm), searchOptions()
                .highlight(HighlightStyle.HTML, "PDFasText")
                .fields("invoice_number", "PDFasText"));
        return result;
    }

    public SearchResult fts_search_company(String searchTerm) {
        final SearchResult result = this.cluster.searchQuery("customer", SearchQuery.term(searchTerm), searchOptions()
                .highlight("customerCompany")
                .fields("customerCompany"));
        return result;
    }

    public JsonObject getDocument(String docKey) {
        JsonObject result = this.collection.get(docKey).contentAs(JsonObject.class);
        return result;
    }

    public <T> void putObjectToCouchbaseCollection(HashMap<String, T> objectsToWrite) {
        objectsToWrite.forEach((key, value) ->
                this.scope.collection("pdf").upsert(key, pojoToJson(value), UpsertOptions.upsertOptions().transcoder(RawJsonTranscoder.INSTANCE)));
    }

    public static String pojoToJson(Object pojo) {
        return new GsonBuilder().serializeNulls().create().toJson(pojo);
    }

    public boolean createBucket(String bucketName, int ramQuota, boolean flushEnabled) {
        BucketManager bucketManager = this.cluster.buckets();
        try {
            bucketManager.createBucket(
                    BucketSettings.create(bucketName)
                            .ramQuotaMB(ramQuota)
                            .flushEnabled(flushEnabled)
                            .numReplicas(0)
            );
        } catch (BucketExistsException e) {
            return false;
        } finally {
            this.bucket = this.cluster.bucket(bucketName);
            return true;
        }
    }

    public static void main(String[] args) {
        // Initiate connection
        Couchbase couchbase = new Couchbase();
        BucketManager bucketManager = couchbase.cluster.buckets();

        // Create PDF bucket
        couchbase.createBucket("pdf", 128, true);

        /*
        try {
            bucketManager.createBucket(
                    BucketSettings.create("pdf")
                        .ramQuotaMB(128)
                        .flushEnabled(true)
                        .numReplicas(0)
                      );
            Bucket bucket = couchbase.cluster.bucket("pdf");
            bucket.waitUntilReady(Duration.ofSeconds(2));
            System.out.println("pdf bucket created");
        } catch (BucketExistsException e) {
            System.err.println("pdf bucket exists");
        }

         */

        // Create PDF scope
        Bucket bucket = couchbase.cluster.bucket("pdf");
        CollectionManager collectionManager = bucket.collections();
        try {
            collectionManager.createScope("pdf");
            System.out.println("pdf scope created");
        } catch (ScopeExistsException e) {
            System.err.println("pdf scope exists");
        }

        // Create PDF collection
        try {
            CollectionSpec collectionSpec = CollectionSpec.create("pdf", "pdf");
            collectionManager.createCollection(collectionSpec);
            System.out.println("pdf collection created");
        } catch (CollectionExistsException e) {
            System.err.println("pdf collection exists");
        }

        // Create secondary indexes
        System.out.println("Creating Index on Customer ID");
        couchbase.exec_n1ql("create index idx_pdf_cust_id on pdf.pdf.pdf(customer_ID);");
        System.out.println("Creating Index on Customer Name");
        couchbase.exec_n1ql("create index idx_pdf_cust_name on pdf.pdf.pdf(customerName);");
        System.out.println("Done!");

    }
}
