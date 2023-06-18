# scrape-tweets-dev-2
Development repo for scraping tweets and market data via RDS. Using optimisations techniques such as (Threading, asynchronous I/O, non-blocking I/O - ConcurrentLinkedQueues, and runnable tasks for making requests and saving tweets to the database. (Rewritten in Java)

Troubleshooting
- Requests drop to 0
    - Maybe you're using too many resources - there's a perfect balance.
        - Having too many worker threads leads to blocks and switching between threads - slowing the system down.
        - Using too much memory and the program can't allocate memory
    - There are no proxies available
    - There are no tasks left
    - You have reached the maximum co-currency
        - Something may be causing a hang - ie. in the request handler, ie. not updating the coroutine count properly
