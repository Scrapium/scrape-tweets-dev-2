package com.scrapium.proxium.loadProxies;

import com.scrapium.DatabaseConnection;
import com.scrapium.proxium.Proxy;
import org.postgresql.util.PSQLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyLoader {

    public void refreshProxies(){
        ProxyLoader.findProxies();
        ProxyLoader.checkProxies();

        System.out.println("\n\nTODO: automate this process...");
        System.out.println("\n> PLEASE NOW USE https://github.com/openproxyspace/unfx-proxy-checker/ to check the proxies..");
        System.out.println("Proxies have been put in ./unchecked_proxies.txt");
        System.out.println("Put the resulting file output in ./checked_proxies.txt\n\n");

        System.out.println("Starting...");
        ProxyLoader.loadProxies();
    }

    public static void findProxies() {

        String filePath = "proxy_list.xml"; // Replace with the path to your XML file

        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            ArrayList<String> http_links = getProxyListEntries(doc, "http_proxy");
            ArrayList<String> sock4_links = getProxyListEntries(doc, "sock4_proxy");
            ArrayList<String> sock5_links = getProxyListEntries(doc, "sock5_proxy");

            ArrayList<String> http_proxies = new ArrayList<String>();
            ArrayList<String> sock5_proxies = new ArrayList<String>();
            ArrayList<String> sock4_proxies = new ArrayList<String>();


            for(String entry: http_links){
                ArrayList<String> proxy_list = getProxies(entry);
                http_proxies.addAll(proxy_list);
                System.out.println("https: (" + proxy_list.size() + ") " + entry + ".");
            }

            System.out.println("===\nTotal Proxies: " + (http_proxies.size() + sock4_proxies.size() + sock5_proxies.size()));
            System.out.println("\nOf which:\n");
            System.out.println("    - (" + http_proxies.size() + ") are https");
            System.out.println("    - (" + sock4_proxies.size() + ") are sock4");
            System.out.println("    - (" + sock5_proxies.size() + ") are sock5");

            try {
                FileWriter writer = new FileWriter("unchecked_proxies.txt");
                for(String proxy : http_proxies){
                    writer.write("https://" + proxy + "\n");
                }
                for(String proxy : sock4_proxies){
                    writer.write("socks4://" + proxy + "\n");
                }
                for(String proxy : sock5_proxies){
                    writer.write("socks5://" + proxy + "\n");
                }

                writer.close();
                System.out.println("Data written to file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getProxies(String url){
        URL oracle = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null){
                list.add(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
        return list;
    }


    private static ArrayList<String> getProxyListEntries(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                list.add(element.getTextContent());
            }
        }
        System.out.println();
        return list;
    }


    public static void checkProxies() {
        System.out.println("CheckProxies is not implemented");
        System.exit(0);
    }

    public static void loadProxies() {
        try (BufferedReader br = new BufferedReader(new FileReader("./checked_proxies.txt"))) {
            String _proxy_entry;
            try (Connection connection = DatabaseConnection.getConnection()) {
                System.out.println("Got connection successfully");

                while ((_proxy_entry = br.readLine()) != null) {

                    // ex. _proxy_entry = http://146.59.147.70:8888

                    String proxy_entry = _proxy_entry.replaceAll("[\\r\\n]+", "");
                    String connString = proxy_entry;


                    /*
                    String connString = proxy_entry;
                    String ip = extractWithPattern(proxy_entry, "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
                    int port = Integer.valueOf(extractWithPattern(proxy_entry, "(?<=:)(\\d+)"));

                    System.out.println(ip);

                    boolean is_socks = Pattern.compile("(?i)socks").matcher(proxy_entry).find();

                    Proxy proxy = new Proxy(
                            -1,
                            connString,
                            ip,
                            port,
                            is_socks,
                            0,
                            0,
                            new Timestamp(0L),
                            "",
                            new Timestamp(0L),
                            0,
                            0,
                            new Timestamp(0L)
                    ); */
                    Proxy proxy = new Proxy(
                        -1,
                            connString,
                        0,
                        0,
                        0,
                        0,
                            new Timestamp(System.currentTimeMillis())
                    );


                    try {
                        addProxyEntry(connection, proxy);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        if (e.getMessage().contains("proxies_conn_string_key")) {
                            System.out.println("Duplicate conn_string entry. Skipping insertion.");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to get connection!");
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }


    // ex. example input = http://146.59.147.70:8888

    public static String extractWithPattern(String input, String pattern) {
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }


    private static void addProxyEntry(Connection connection, Proxy proxy) throws SQLException {
        String insertSQL = "INSERT INTO test_proxy (connection_string, usage_count, success_count, failed_count, fail_streak, cooldown_until) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

        preparedStatement.setString(1, proxy.getConnectionString()); // Set connection_string value
        preparedStatement.setInt(2, 0); // Set usage_count value
        preparedStatement.setInt(3, 0); // Set success_count value
        preparedStatement.setInt(4, 0); // Set failed_count value
        preparedStatement.setInt(5, 0); // Set fail_streak value
        preparedStatement.setTimestamp(6, null); // Set cooldown_until value (null as an example)

        int affectedRows = preparedStatement.executeUpdate();
        System.out.println("Inserted " + affectedRows + " row(s) into the proxies table.");
    }
}
