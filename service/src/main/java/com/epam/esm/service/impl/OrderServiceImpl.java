package com.epam.esm.service.impl;

import com.epam.esm.dto.OrderDto;
import com.epam.esm.entity.GiftCertificatePurchase;
import com.epam.esm.entity.Order;
import com.epam.esm.entity.User;
import com.epam.esm.mapper.impl.OrderMapper;
import com.epam.esm.mapper.impl.UserMapper;
import com.epam.esm.repository.GiftCertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.UserRepository;
import com.epam.esm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The type Order service.
 *
 * @author YanaV
 * @project GiftCertificate
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Value("${max.result.amount}")
    private int maxResultAmount;
    private int lastPage;
    private final OrderRepository repository;
    private final GiftCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final OrderMapper mapper;
    private final UserMapper userMapper;

    /**
     * Instantiates a new Order service.
     *
     * @param repository            the repository
     * @param certificateRepository the certificate repository
     * @param userRepository        the user repository
     * @param mapper                the mapper
     * @param userMapper            the user mapper
     */
    @Autowired
    public OrderServiceImpl(OrderRepository repository,
                            GiftCertificateRepository certificateRepository,
                            UserRepository userRepository,
                            @Qualifier("orderServiceMapper") OrderMapper mapper,
                            @Qualifier("userServiceMapper") UserMapper userMapper) {
        this.repository = repository;
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public boolean create(OrderDto orderDto) {
        boolean allCertificatesExist = orderDto.getCertificates()
                .stream()
                .allMatch(c -> certificateRepository.findById(c.getCertificate().getId()).isPresent());
        long userId = orderDto.getUser().getId();
        Optional<User> user = userRepository.findById(userId);
        if (allCertificatesExist && user.isPresent()) {
            Set<GiftCertificatePurchase> certificates = orderDto.getCertificates()
                    .stream()
                    .map(c -> new GiftCertificatePurchase(certificateRepository.findById(c.getCertificate().getId()).get(), c.getAmount()))
                    .collect(Collectors.toSet());
            BigDecimal orderCost = BigDecimal.valueOf(certificates.stream()
                    .map(c -> c.getCertificate().getPrice().multiply(BigDecimal.valueOf(c.getAmount())))
                    .mapToLong(BigDecimal::longValue)
                    .sum());
            if (orderCost.compareTo(user.get().getBalance()) <= 0) {
                Order order = Order.builder()
                        .user(user.get())
                        .certificates(certificates)
                        .cost(orderCost)
                        .build();
                order.getCertificates().forEach(c -> c.setOrder(order));
                BigDecimal newBalance = user.get().getBalance().subtract(orderCost);
                userRepository.updateBalance(user.get().getId(), newBalance);
                repository.create(order);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean update(OrderDto orderDto) {
        Order order = mapper.mapToEntity(orderDto);
        repository.update(order);
        return true;
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        Optional<Order> order = repository.findById(id);
        if (order.isPresent()) {
            repository.delete(order.get());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Set<OrderDto> findAll(int page) {
        int firstElementNumber = getFirstElementNumber(page, maxResultAmount);
        Set<Order> foundOrders = repository.findAll(firstElementNumber);
        lastPage = repository.getLastPage();
        Set<OrderDto> orders = foundOrders.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toSet());
        return includeUserToOrder(new ArrayList<>(foundOrders), orders);
    }

    @Override
    public Optional<OrderDto> findById(long id) {
        Optional<Order> order = repository.findById(id);
        if (order.isPresent()) {
            OrderDto orderDto = mapper.mapToDto(order.get());
            orderDto.setUser(userMapper.mapToDto(order.get().getUser()));
            return Optional.of(orderDto);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Set<OrderDto> findAllByUser(int page, long userId) {
        int firstElementNumber = getFirstElementNumber(page, maxResultAmount);
        Set<Order> foundOrders = repository.findAllByUser(firstElementNumber, userId);
        foundOrders.forEach(o -> o.setCertificates(new LinkedHashSet<>()));
        lastPage = repository.getLastPage();
        Set<OrderDto> orders = foundOrders.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toSet());
        return includeUserToOrder(new ArrayList<>(foundOrders), orders);
    }

    @Override
    public Set<OrderDto> findBySeveralParameters(int page, OrderDto orderDto, List<Integer> userIds,
                                                 List<Integer> certificateIds) {
        Order order = mapper.mapToEntity(orderDto);
        if (certificateIds != null) {
            Set<GiftCertificatePurchase> certificates = certificateIds.stream()
                    .map(GiftCertificatePurchase::new)
                    .collect(Collectors.toSet());
            order.setCertificates(certificates);
        }
        int firstElementNumber = getFirstElementNumber(page, maxResultAmount);
        Set<Order> foundOrders = repository.findBySeveralParameters(firstElementNumber, order, userIds);
        lastPage = repository.getLastPage();
        Set<OrderDto> orders = foundOrders.stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toSet());
        return includeUserToOrder(new ArrayList<>(foundOrders), orders);
    }

    private Set<OrderDto> includeUserToOrder(List<Order> foundOrders, Set<OrderDto> orders) {
        AtomicInteger index = new AtomicInteger();
        orders.forEach(o -> {
            User user = foundOrders.get(index.getAndIncrement()).getUser();
            o.setUser(userMapper.mapToDto(user));
            o.getUser().setOrders(null);
        });
        return orders;
    }

    @Override
    public int getLastPage() {
        return lastPage;
    }
}