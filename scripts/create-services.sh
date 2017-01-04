cf cups config-server -p '{"uri":"http://config-server.cfapps.haas-85.pez.pivotal.io"}'
cf cups service-registry -p '{"uri":"http://eureka.cfapps.haas-85.pez.pivotal.io"}'
cf cups circuit-breaker -p '{"uri":"http://hystrix-dashboard.cfapps.haas-85.pez.pivotal.io"}'