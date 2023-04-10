package com.scrapium;

public class Main {

    public static void main(String[] args) {

        Scraper scraper = new Scraper(4, 400);
        scraper.scrape();

        /*

        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

        client.start();


        //////////////////////////////





        final BasicHttpRequest request = BasicRequestBuilder.get()
                .setHttpHost( new HttpHost("httpbin.org") )
                .setPath("/headers")
                .build();

        System.out.println("Executing request " + request);
        final Future<Void> future = client.execute(
                new BasicRequestProducer(request, null),
                new AbstractCharResponseConsumer<Void>() {

                    @Override
                    protected void start(
                            final HttpResponse response,
                            final ContentType contentType) throws HttpException, IOException {
                        System.out.println(request + "->" + new StatusLine(response));
                    }

                    @Override
                    protected int capacityIncrement() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    protected void data(final CharBuffer data, final boolean endOfStream) throws IOException {
                        while (data.hasRemaining()) {
                            System.out.print(data.get());
                        }
                        if (endOfStream) {
                            System.out.println();
                        }
                    }

                    @Override
                    protected Void buildResult() throws IOException {
                        return null;
                    }

                    @Override
                    public void failed(final Exception cause) {
                        System.out.println(request + "->" + cause);
                    }

                    @Override
                    public void releaseResources() {
                    }

                }, null);

        System.out.println("test");

        // wait until done

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // RUN THIS TO CLOSE CONNECTIONS.
        // client.close(CloseMode.GRACEFUL);

         */

    }
}