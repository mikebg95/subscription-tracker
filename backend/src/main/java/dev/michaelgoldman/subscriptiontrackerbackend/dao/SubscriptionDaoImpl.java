package dev.michaelgoldman.subscriptiontrackerbackend.dao;

import dev.michaelgoldman.subscriptiontrackerbackend.exception.SubscriptionAlreadyExistsException;
import dev.michaelgoldman.subscriptiontrackerbackend.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionDaoImpl implements SubscriptionDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Subscription> rowMapper = (rs, _) -> new Subscription(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getBigDecimal("price")
    );

    @Override
    public Subscription save(Subscription subscription) {
        String sql = "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id, name, price";

        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, subscription.getName(), subscription.getPrice());
        } catch (DuplicateKeyException e) {
            throw new SubscriptionAlreadyExistsException("A subscription with that name already exists.", e);
        }
    }

    @Override
    public List<Subscription> findAll() {
        String sql = "SELECT id, name, price FROM subscriptions ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        String sql = "SELECT id, name, price FROM subscriptions WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM subscriptions WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public int update(Subscription subscription) {
        String sql = "UPDATE subscriptions SET name = ?, price = ? WHERE id = ?";

        try {
            return jdbcTemplate.update(sql, subscription.getName(), subscription.getPrice(), subscription.getId());
        } catch (DuplicateKeyException e) {
            throw new SubscriptionAlreadyExistsException("A subscription with that name already exists.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM subscriptions";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return Objects.requireNonNull(count);
    }

    @Override
    public BigDecimal total() {
        String sql = "SELECT COALESCE(SUM(price), 0) FROM subscriptions";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }
}
