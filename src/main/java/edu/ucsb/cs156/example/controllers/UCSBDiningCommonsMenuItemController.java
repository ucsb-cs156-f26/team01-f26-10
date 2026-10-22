package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbdiningcommonsmenuitems")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB dining commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();
    return items;
  }

  /**
   * Create a new item
   *
   * @param diningCommonsCode the dining commons code
   * @param name the name of the item
   * @param station the station serving the item
   * @return the saved item
   */
  @Operation(summary = "Create a new item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItem();
    ucsbDiningCommonsMenuItem.setDiningCommonsCode(diningCommonsCode);
    ucsbDiningCommonsMenuItem.setName(name);
    ucsbDiningCommonsMenuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedUCSBDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return savedUCSBDiningCommonsMenuItem;
  }

  /**
   * Get a single dining commons menu item by id
   *
   * @param id the id of the dining commons menu item
   * @return a UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Get a single date")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return item;
  }

  /**
   * Update a single dining commons menu item
   *
   * @param id id of the dining commons menu item to update
   * @param incoming the new dining commons menu item
   * @return the updated dining commons menu item object
   */
  @Operation(summary = "Update a single item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem item =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    item.setDiningCommonsCode(incoming.getDiningCommonsCode());
    item.setName(incoming.getName());
    item.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(item);

    return item;
  }
}
