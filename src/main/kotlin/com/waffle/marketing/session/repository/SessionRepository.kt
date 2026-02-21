package com.waffle.marketing.session.repository

import com.waffle.marketing.session.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SessionRepository : JpaRepository<Session, UUID>
