package com.epam.esm.repository.impl;

import com.epam.esm.builder.GiftCertificateQueryBuilder;
import com.epam.esm.builder.OrderQueryBuilder;
import com.epam.esm.entity.Order;
import com.epam.esm.mapper.OrderMapper;
import com.epam.esm.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The type Order repository.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private static final String INSERT_ORDER = "INSERT INTO orders (id_user, id_certificate, cost, create_date) " +
            "VALUES (?, ?, ?, now(3))";
    private static final String UPDATE_ORDER = "UPDATE orders SET ";
    private static final String DELETE_ORDER = "DELETE FROM orders WHERE id = ?";
    private static final String SELECT_ORDERS = "SELECT orders.id, users.id, login, surname, users.name, balance, " +
            "certificates.id, certificates.name, description, price, duration, certificates.create_date, last_update_date, " +
            "cost, orders.create_date, tags.id, tags.name FROM orders " +
            "JOIN gift_certificates certificates on orders.id_certificate = certificates.id " +
            "JOIN users on orders.id_user = users.id " +
            "JOIN certificate_purchase on certificates.id = certificate_purchase.id_certificate " +
            "JOIN tags on certificate_purchase.id_tag = tags.id";
    private final JdbcTemplate template;
    private final OrderMapper orderMapper;

    /**
     * Instantiates a new Order repository.
     *
     * @param dataSource  the data source
     * @param orderMapper the order mapper
     */
    @Autowired
    public OrderRepositoryImpl(DataSource dataSource, OrderMapper orderMapper) {
        this.template = new JdbcTemplate(dataSource);
        this.orderMapper = orderMapper;
    }

    @Override
    public long create(Order order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, order.getUser().getId());
            statement.setLong(2, order.getCertificate().getId());
            statement.setBigDecimal(3, order.getCost());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        } else {
            return 0;
        }
    }

    @Override
    public void update(Order order) {
        template.update(new OrderQueryBuilder(UPDATE_ORDER)
                .addUserIdParameter(order.getUser().getId())
                .checkQueryEnding()
                .addCertificateIdParameter(order.getCertificate().getId())
                .checkQueryEnding()
                .addCostParameter(order.getCost())
                .addWhereClause()
                .addIdParameter(order.getId())
                .build());
    }

    @Override
    public void delete(long id) {
        template.update(DELETE_ORDER, id);
    }

    @Override
    public Set<Order> findAll() {
        List<Order> orders = template.query(SELECT_ORDERS, orderMapper);
        return new LinkedHashSet<>(orders);
    }

    @Override
    public Optional<Order> findById(long id) {
        List<Order> orders = template.query(new GiftCertificateQueryBuilder(SELECT_ORDERS)
                .addWhereClause()
                .addIdParameter(id)
                .build(), orderMapper);
        return orders.stream().findFirst();
    }

    @Override
    public Set<Order> findAllOrdersByUser(long userId) {
        List<Order> orders = template.query(SELECT_ORDERS, orderMapper);
        return new LinkedHashSet<>(orders);
    }

    @Override
    public Set<Order> findOrdersBySeveralParameters(Order order) {
        List<Order> orders = template.query(
                new OrderQueryBuilder(SELECT_ORDERS)
                        .addWhereClause(order.getId())
                        .addIdParameter(order.getId())
                        .addIdParameter(order.getId())
                        .addUserSurnameLikeParameter(order.getUser().getSurname())
                        .addUserNameLikeParameter(order.getUser().getName())
                        .addCertificateIdParameter(order.getCertificate().getId())
                        .addCertificateNameLikeParameter(order.getCertificate().getName())
                        .addCostParameter(order.getCost())
                        .addCreateDateParameter(order.getCreateDate())
                        .build(), orderMapper);
        return new LinkedHashSet<>(orders);
    }
}
