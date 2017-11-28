# spring-cloud-api-gateway-oauth2

Project: Simple microservice example of two microservices secured via OAuth2.
Ui shows a greeting from resource.
![GitHub Logo](/images/ui.png)

The microservices consume Pivotal Spring Cloud Services and Pivotal Single-Sign-On Service.  

- [Pivotal Single-Sign-On Services](https://docs.pivotal.io/p-identity/1-3/index.html)
- [Pivotal Spring Cloud Services(Eureka, Hystrix, Config Server) ](http://docs.pivotal.io/spring-cloud-services/1-3/common/index.html)
microservice registry
![GitHub Logo](/images/registry.png)
circuit breaker for microservice fault tolerance and monitoring.
![GitHub Logo](/images/hystrix.png)

Pivotal provided services consumed by the microservices.
![GitHub Logo](/images/services.png)

Microservices: ui-oauth2 and resource-oauth2 spring boot applications.

[ui-oauth2](/ui-oauth2):
- login ui.
- authentication and authorization managed by Pivotal SSO.
- two-factor authentication would be supported by integration (SAML, ...) with external identity providers like OKTA and many others.
- ui sends a request via spring cloud zuul to resource.

[resource-oauth2](/resource-oauth2):
- resource answers if the user has permission (right OAuth2 scope).
![GitHub Logo](/images/scope.png)

# Prerequisites
- a Pivotal CF instance with Pivotal Single-Sign-On service and Pivotal Spring Cloud Services

# Run it on PCF
- clone the repo
- cd spring-cloud-api-gateway-oauth2
- git branch sso
- edit CF_TARGET in [manifest.yml](/manifest.yml) to point to your PCF api endpoint
- mvn package
- [Manage Service Plans](http://docs.pivotal.io/p-identity/1-3/manage-service-plans.html)
  ![GitHub Logo](/images/plan.png)
- sh
    [create-services_pcf.sh](/create-services_pcf.sh)    
- cf push
- (Optional) [Configure Identity Providers](http://docs.pivotal.io/p-identity/1-3/configure-id-providers.html#config-int-store)
- [Add Users to the Internal User Store](http://docs.pivotal.io/p-identity/1-3/configure-id-providers.html#add-to-int)
![GitHub Logo](/images/admin.png)
![GitHub Logo](/images/user.png)
- [Manage Resources](http://docs.pivotal.io/p-identity/1-3/manage-resources.html)
  ![GitHub Logo](/images/permission.png)
  ![GitHub Logo](/images/scopes.png)

# Cross microservice traces
[Pivotal CF Metrics](https://docs.pivotal.io/pcf-metrics/1-3/index.html)

![GitHub Logo](/images/trace.png)

# C2C
```
cf app resource --guid
0d58d899-f055-46b7-9c3d-95ea5c0174de
Griffon:spring-cloud-api-gateway-oauth2 dflick$ cf app ui --guid
4fa656d5-e5ef-48b9-9564-0b63bafa34da
cf curl -H "Content-Type: application/json" -X POST /networking/v0/external/policies -d '{"policies": [{"source": {"id": "4fa656d5-e5ef-48b9-9564-0b63bafa34da"},"destination": {"id": "0d58d899-f055-46b7-9c3d-95ea5c0174de","protocol": "tcp","port": 8080}}]}'
´´´
