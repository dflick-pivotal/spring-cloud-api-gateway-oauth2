cf cups config-server -p '{"uri":"http://config-server.local2.pcfdev.io"}'
cf cups service-registry -p '{"uri":"http://eureka.local2.pcfdev.io"}'
cf cups circuit-breaker -p '{"uri":"http://hystrix-dashboard.local2.pcfdev.io"}'