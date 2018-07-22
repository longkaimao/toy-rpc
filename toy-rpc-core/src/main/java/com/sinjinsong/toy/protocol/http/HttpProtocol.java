package com.sinjinsong.toy.protocol.http;

import com.sinjinsong.toy.common.exception.RPCException;
import com.sinjinsong.toy.config.*;
import com.sinjinsong.toy.protocol.api.Exporter;
import com.sinjinsong.toy.protocol.api.Invoker;
import com.sinjinsong.toy.protocol.api.support.AbstractProtocol;
import com.sinjinsong.toy.transport.http.server.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author sinjinsong
 * @date 2018/7/18
 */
@Slf4j
public class HttpProtocol extends AbstractProtocol {

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker, ServiceConfig<T> serviceConfig) throws RPCException {
        HttpExporter<T> exporter = new HttpExporter<>();
        exporter.setInvoker(invoker);
        exporter.setServiceConfig(serviceConfig);
        putExporter(invoker.getInterface(), exporter);
        // export
        try {
            int port = serviceConfig.getProtocolConfig().getPort() != null ? serviceConfig.getProtocolConfig().getPort() : serviceConfig.getProtocolConfig().DEFAULT_PORT;
            serviceConfig.getRegistryConfig().getRegistryInstance().register(InetAddress.getLocalHost().getHostAddress() + ":" + port, serviceConfig.getInterfaceName());
        } catch (UnknownHostException e) {
            throw new RPCException("获取本地Host失败", e);
        }
        return exporter;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type) throws RPCException {
        HttpInvoker<T> invoker = new HttpInvoker<>();
        invoker.setInterfaceClass(type);
       return invoker;
    }

    @Override
    public void doOnApplicationLoadComplete(ApplicationConfig applicationConfig, ClusterConfig clusterConfig, RegistryConfig registry, ProtocolConfig protocolConfig) {
        log.info("http protocol doOnApplicationLoadComplete...");
        if (isExporterExists()) {
            HttpServer httpServer = new HttpServer();
            httpServer.init(applicationConfig, clusterConfig, registry, protocolConfig);
            httpServer.run();
        }
    }
}