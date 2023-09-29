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
![1](https://github.com/monk1108/Siyuan_Review/assets/61319274/0d68c42d-6b3d-46d9-885b-2705092ce05c)

**Pic 1. User login and user info**


![2](https://github.com/monk1108/Siyuan_Review/assets/61319274/38aa715f-7f78-477c-9b13-f3f057319048)

**Pic 2. APP home page, featured merchants and User evaluation of store visit.**
