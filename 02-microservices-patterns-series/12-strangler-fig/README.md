# Pattern 12: Strangler Fig Pattern (Monolith Migration)

## 1. Architectural Problem
A "Big-Bang Rewrite" of a monolithic application into microservices carries catastrophic failure risk. Development freezes for months, legacy edge-cases get lost, and shifting traffic overnight creates massive regressions.

## 2. The Solution
The **Strangler Fig Pattern** places an intercepting API Gateway / Reverse Proxy in front of the legacy monolith. Bounded contexts are carved out into standalone microservices one by one, shifting traffic incrementally until the monolith is strangled and retired.# Pattern 12: Strangler Fig Pattern (Monolith Migration)

## 1. Architectural Problem
A "Big-Bang Rewrite" of a monolithic application into microservices carries catastrophic failure risk. Development freezes for months, legacy edge-cases get lost, and shifting traffic overnight creates massive regressions.

## 2. The Solution
The **Strangler Fig Pattern** places an intercepting API Gateway / Reverse Proxy in front of the legacy monolith. Bounded contexts are carved out into standalone microservices one by one, shifting traffic incrementally until the monolith is strangled and retired.