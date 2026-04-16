# Part 1 - Hands-on Implementation

1. Design and implement a bulk-insert REST endpoint to efficiently save multiple coupons.
2. Refactor the API to implement a robust, global error handling mechanism.

# Part 2 - Architecture & Code Review:

1. **Scalability**: Assuming the database grows to millions of records, what stability risks do you identify in the
   current
   GET endpoint, and how would you resolve them
2. **Persistence**: Review the CouponRepository and the current cleanup logic. How could you simplify the code and
   optimize the data expiration using framework or native database features?
3. **Resilience**: If a client submits an excessively large payload to your new batch endpoint, what system bottlenecks
   could occur? How would you protect the service?