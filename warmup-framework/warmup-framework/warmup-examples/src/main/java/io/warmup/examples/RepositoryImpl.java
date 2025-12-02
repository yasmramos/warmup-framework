/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.warmup.examples;

import java.util.ArrayList;
import java.util.List;
import io.warmup.framework.annotation.Component;

@Component
public class RepositoryImpl implements Repository {

    private final List<String> storage = new ArrayList<>();

    @Override
    public void save(String data) {
        storage.add(data);
    }

    @Override
    public String findById(String id) {
        return storage.stream()
                .filter(data -> data.contains(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> findAll() {
        return new ArrayList<>(storage);
    }
}
