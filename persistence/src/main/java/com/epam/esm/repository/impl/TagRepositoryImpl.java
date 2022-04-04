package com.epam.esm.repository.impl;

import com.epam.esm.entity.Tag;
import com.epam.esm.mapper.TagMapper;
import com.epam.esm.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
public class TagRepositoryImpl implements TagRepository {
    private static final String INSERT_TAG = "INSERT INTO tag (name) VALUES (?)";
    private static final String DELETE_TAG = "DELETE FROM tag WHERE id = ?";
    private static final String SELECT_ALL_TAGS = "SELECT id, name FROM tag";
    private static final String SELECT_TAG_BY_ID = "SELECT id, name FROM tag WHERE id = ?";
    private static final String SELECT_TAG_BY_NAME = "SELECT id, name FROM tag WHERE name = ?";
    private final JdbcTemplate template;

    @Autowired
    public TagRepositoryImpl(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public long create(Tag tag) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_TAG, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, tag.getName());
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public boolean delete(long id) {
        template.update(DELETE_TAG, id);
        return true;
    }

    @Override
    public Set<Tag> findAll() {
        return new HashSet<>(template.query(SELECT_ALL_TAGS, new TagMapper()));
    }

    @Override
    public Optional<Tag> findById(long id) {
        try {
            Tag tag = template.queryForObject(SELECT_TAG_BY_ID, new TagMapper(), id);
            return Optional.ofNullable(tag);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Tag> findByName(String name) {
        try {
            Tag tag = template.queryForObject(SELECT_TAG_BY_NAME, new TagMapper(), name);
            return Optional.ofNullable(tag);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
