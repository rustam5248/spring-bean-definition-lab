package uz.example.beanlab.order;

import java.time.Instant;

public record Order(String id, String item, Instant createAt) {}
