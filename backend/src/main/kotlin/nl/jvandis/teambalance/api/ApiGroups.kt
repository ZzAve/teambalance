package nl.jvandis.teambalance.api

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('admin')")
annotation class Admin

annotation class Public
