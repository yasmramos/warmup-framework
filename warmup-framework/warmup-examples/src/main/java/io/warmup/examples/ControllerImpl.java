/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.warmup.examples;

import io.warmup.framework.annotation.Component;

@Component
public class ControllerImpl implements Controller {

    private Service service;

    @Override
    public String handleRequest(String request) {
        if (service == null) {
            throw new IllegalStateException("Service not set");
        }
        return service.process(request);
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }
}
