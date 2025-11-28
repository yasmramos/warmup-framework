/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.warmup.examples;

import io.warmup.framework.annotation.Component;

@Component
public class FactoryImpl implements Factory {
    @Override
    public Service createService() {
        return new ServiceImpl();
    }
    
    @Override
    public Repository createRepository() {
        return new RepositoryImpl();
    }
    
    @Override
    public Controller createController() {
        return new ControllerImpl();
    }
}