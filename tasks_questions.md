### Tasks:

1. Implement a new endpoint for saving multiple coupons at once
2. Improve error handling in REST controller

### Questions:

1. At a certain point the service will manage high amount of coupons. What concerns do you have with the current
   endpoints?
2. Have a look at the CouponRepository. Do you see any potential for improvements?
3. What problem could occur, if someone uses the new endpoint to insert a huge amount of coupons into the database?