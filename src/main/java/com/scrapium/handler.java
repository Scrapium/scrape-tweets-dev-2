package com.scrapium;

import org.asynchttpclient.*;

import java.io.IOException;

public class handler implements AsyncHandler<Integer> {
    private final AsyncHttpClient c;
    private final TweetThreadTaskProcessor processor;
    private Integer status;

    public handler(AsyncHttpClient c, TweetThreadTaskProcessor tweetThreadTaskProcessor) {
        this.c = c;
        this.processor = tweetThreadTaskProcessor;
    }


    @Override
        public AsyncHandler.State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
            status = responseStatus.getStatusCode();
            if(status >= 200 && status < 400){
                this.processor.getScraper().logger.increaseSuccessRequestCount();
            } else {
                this.processor.getScraper().logger.increaseFailedRequestCount();
            }
            //System.out.println(status);
            //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
            return AsyncHandler.State.ABORT;
        }

        @Override
        public AsyncHandler.State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
            return AsyncHandler.State.ABORT;
        }


        @Override
        public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
            //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
            return AsyncHandler.State.ABORT;
        }
        @Override
        public Integer onCompleted() throws Exception{

            //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
            return 200;

        }

        @Override
        public void onThrowable(Throwable t) {
            //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
            //t.printStackTrace();
        }

}
