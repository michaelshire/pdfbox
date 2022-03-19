package com.couchbase.se;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.search.result.SearchResult;
import com.couchbase.client.java.search.result.SearchRow;
import com.couchbase.se.models.Invoice;
import com.couchbase.se.services.Couchbase;
import com.couchbase.se.services.PDF;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class CouchbasePDF {

    // The following
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class MyHandler2 implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the other response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class CouchbaseHttp implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Integer rowNum = 0;
            StringBuilder html = new StringBuilder("<html><body>");
            Couchbase couchbase = new Couchbase();
            final SearchResult result = couchbase.fts_search("blade");
            html.append(String.format("<h3>Number of Documents Found: %d<h3>\n", result.rows().size()));
            html.append("<br><hr><br>");
            for (SearchRow row : result.rows()) {
                JsonObject jsonObject = couchbase.getDocument(row.id());
                html.append(String.format("<h4>Document: %d<h4>\n<p>Fragment: %s</p>\n<iframe width=\"100%\" height=\"300\" style=\"border:none\" src=\"%s\"></iframe>\n</body></html>",
                        ++rowNum, row.fragments().get("PDFasText"), jsonObject.get("PDFasPDF")));
            }
            exchange.sendResponseHeaders(200, html.length());
            OutputStream os = exchange.getResponseBody();
            os.write(html.toString().getBytes());
            os.close();
            couchbase.disconnect();
        }
    }


    public static void main(String[] args) {
        String invoice_date;
        Integer invoice_number;
        Integer customer_ID;
        String due_date;
        String customerName;
        String customerCompany;
        String customerStreet;
        String customerCityStateZip;
        String customerPhone;
        String PDFasText;
        String PDFasPDF;
        HashMap<String, Invoice> invoices = new HashMap<>();

        for(int i= args.length-1; i>=0; i--){
            PDF pdf = new PDF(args[i]);
            invoice_date = pdf.getTextByArea(474,84,70,14);
            invoice_number = Integer.parseInt(pdf.getTextByArea(474,98, 70, 14));
            customer_ID = Integer.parseInt(pdf.getTextByArea(474,112, 70, 14));
            due_date = pdf.getTextByArea(474,126, 70, 14);
            customerName = pdf.getTextByArea(67,176,200,14);
            customerCompany = pdf.getTextByArea(67,190,200,14);
            customerStreet = pdf.getTextByArea(67,204,200,14);
            customerCityStateZip = pdf.getTextByArea(67,218,200,14);
            customerPhone = pdf.getTextByArea(67,232,200,14);
            PDFasText = pdf.getAllText();
            PDFasPDF = pdf.pdfToBase64();
            invoices.put(String.format("invoice::%d::%d", customer_ID, invoice_number), new Invoice(invoice_date, invoice_number, customer_ID, due_date, customerName, customerCompany,
                    customerStreet, customerCityStateZip, customerPhone, PDFasText, PDFasPDF ));
        }
        Couchbase couchbase = new Couchbase();
        couchbase.getBucket();
        couchbase.putObjectToCouchbaseCollection(invoices);

        // Could use mutation result to query FTS index, but we'll just sleep for simplicity
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("couchbase.html"));
            StringBuilder html = new StringBuilder("<html><body>");
            int rowNum = 0;

            // Running FTS Search (for 'blade')
            final SearchResult result = couchbase.fts_search("blade");
//        final SearchResult result = couchbase.fts_search("compressor");
            for (SearchRow row : result.rows()) {
                JsonObject jsonObject = couchbase.getDocument(row.id());
                Map<String, List<String>> fragments = row.fragments();
                String fragment = row.fragments().get("PDFasText").toString().replace("…", "");
                String pdfAsPDF = jsonObject.getString("PDFasPDF");
                String format="<h4>Document: %d</h4>\n<pre>Fragment: %s</pre>\n<iframe width=\"70%%\" height=\"300\" style=\"border:single; display:block; margin: 0 auto; padding: 10;\" src=\"data:application/pdf;base64,%s\"></iframe>\n";
                System.out.printf("Row (%s): Invoice: %s\nFragment: %s\n\n", row.id(), jsonObject.get("invoice_number"), fragments.get("PDFasText"));
                html.append(String.format(format, ++rowNum, fragment, pdfAsPDF));
            }
            html.append("</body></html>");
            writer.write(html.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*
        final SearchResult result1 = couchbase.fts_search_company("company");
        for (SearchRow rowCompany : result1.rows()) {
            JsonObject jsonObjectField = rowCompany.fieldsAs(JsonObject.class);
            JsonObject jsonObject = couchbase.getDocument(rowCompany.id());
            Map<String, List<String>> companyFragment = rowCompany.fragments();
            System.out.printf("Row (%s): %s\n", rowCompany.id(), rowCompany.fieldsAs(JsonObject.class).toString());
        }

         */


        /*
        System.out.println("Launching Web Server...");

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/test", new MyHandler());
        server.createContext("/test2", new MyHandler2());
        server.createContext("/couchbase", new CouchbaseHttp());
        server.setExecutor(null); // creates a default executor
        server.start();
         */
        couchbase.disconnect();

    }
}
