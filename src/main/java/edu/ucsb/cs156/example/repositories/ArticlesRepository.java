package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Articles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/** The ArticlesRepository is a repository for Articles entities. */
@Repository
@RepositoryRestResource(exported = false)
public interface ArticlesRepository extends CrudRepository<Articles, Long> {}
