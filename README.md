# Siyuan_Review
A review platform focusing on the culinary and scenic offerings within SJTU’s campus using SpringBoot and MyBatis-Plus.
## Shining Points:
- Implemented Redis caching for store data and effectively resolved cache breakdown issues through techniques such as
mutex and cache expiration.
- Leveraged Redis to enable flash sales of products, effectively mitigating overselling issues using optimistic locking.
Integrated Lua scripts with message queues to achieve asynchronous flash sale processes.
- Conducted comprehensive stress tests using Jmeter to address concerns like cache penetration and one-person-one-order
scenarios.

## Demonstration:
![1](https://github.com/monk1108/Siyuan_Review/assets/61319274/52aa3561-054a-4017-83e9-de7988fa38ff)

**Pic 1. User login and user info**

![2](https://github.com/monk1108/Siyuan_Review/assets/61319274/44eb443b-5845-445e-8a38-cb1d44eee090)

**Pic 2. APP home page, featured merchants and User evaluation of store visit.**
