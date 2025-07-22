package com.example.smartstudy.repository;

import com.example.smartstudy.model.StudyFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyFileRepository extends JpaRepository<StudyFile, Long> {
    List<StudyFile> findByUserId(Long userId);
}