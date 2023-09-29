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
![37a795f6188876014a5537c344012a9c](https://github.com/monk1108/Siyuan_Review/assets/61319274/ee20021c-f5d5-415b-bbf0-a2c16dbffe74)


**Pic 1. User login and user info**

![301093c43e8b6e7b50666947575b5e39](https://github.com/monk1108/Siyuan_Review/assets/61319274/afaf97ae-d3fd-490f-93b9-3f67133215ff)


**Pic 2. APP home page, featured merchants and User evaluation of store visit**
