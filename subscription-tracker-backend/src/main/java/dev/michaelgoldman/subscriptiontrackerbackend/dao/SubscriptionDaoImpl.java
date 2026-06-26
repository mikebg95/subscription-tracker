package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubscriptionDaoImpl implements SubscriptionDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Subscription> rowMapper = (rs, _) -> new Subscription(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getBigDecimal("price")
    );

    public SubscriptionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Subscription save(Subscription subscription) {
        String sql = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id, name, price";
        return jdbcTemplate.queryForObject(sql, rowMapper, subscription.getName(), subscription.getPrice());
    }

    @Override
    public List<Subscription> findAll() {
        String sql = "SELECT id, name, price FROM subscriptions ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
