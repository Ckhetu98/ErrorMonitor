package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.TrainingDto;
import com.tpms.entity.Training;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.TrainingRepository;
import com.tpms.repository.UserRepository;
import com.tpms.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<List<TrainingDto>> getAll() {
        List<TrainingDto> list = trainingRepository.findAll().stream()
                .map(t -> modelMapper.map(t, TrainingDto.class))
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<List<TrainingDto>> getByStudent(Long studentId) {
        var enrolls = enrollmentRepository.findByStudentId(studentId);
        List<TrainingDto> list = enrolls.stream()
                .map(e -> {
                    TrainingDto dto = modelMapper.map(e.getTraining(), TrainingDto.class);
                    return dto;
                }).collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<TrainingDto> create(TrainingDto dto) {
        Training t = modelMapper.map(dto, Training.class);
        if (dto.getTrainerId() != null) {
            User u = userRepository.findById(dto.getTrainerId()).orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
            t.setTrainer(u);
        }
        Training saved = trainingRepository.save(t);
        return new ApiResponse<>(java.time.Instant.now(), "Created", "SUCCESS", modelMapper.map(saved, TrainingDto.class));
    }

    @Override
    public ApiResponse<TrainingDto> update(Long id, TrainingDto dto) {
        Training existing = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));
        
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setMaxParticipants(dto.getMaxParticipants());
        existing.setStatus(dto.getStatus());
        
        if (dto.getTrainerId() != null) {
            User trainer = userRepository.findById(dto.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
            existing.setTrainer(trainer);
        }
        
        Training updated = trainingRepository.save(existing);
        return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", modelMapper.map(updated, TrainingDto.class));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));
        
        // Delete all enrollments for this training first
        enrollmentRepository.deleteByTrainingId(id);
        
        // Now delete the training
        trainingRepository.delete(training);
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }

    @Override
    public ApiResponse<Void> enroll(Long studentId, Long trainingId) {
        if (enrollmentRepository.findByTrainingIdAndStudentId(trainingId, studentId).isPresent()) {
            return new ApiResponse<>(java.time.Instant.now(), "Already enrolled", "SUCCESS", null);
        }
        User student = userRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Training t = trainingRepository.findById(trainingId).orElseThrow(() -> new ResourceNotFoundException("Training not found"));
        
        com.tpms.entity.TrainingEnrollment ent = new com.tpms.entity.TrainingEnrollment();
        ent.setStudent(student);
        ent.setTraining(t);
        ent.setStatus("ENROLLED");
        enrollmentRepository.save(ent);
        
        // Update enrolled count based on actual enrollments
        int currentEnrollments = enrollmentRepository.findByTrainingId(trainingId).size();
        t.setEnrolledCount(currentEnrollments);
        trainingRepository.save(t);
        
        return new ApiResponse<>(java.time.Instant.now(), "Enrolled successfully", "SUCCESS", null);
    }

    @Override
    public ApiResponse<List<Object>> getEnrolledStudents(Long trainingId) {
        var enrollments = enrollmentRepository.findByTrainingId(trainingId);
        
        // Update enrolled count in training
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found"));
        training.setEnrolledCount(enrollments.size());
        trainingRepository.save(training);
        
        List<Object> students = enrollments.stream()
                .map(enrollment -> {
                    var student = enrollment.getStudent();
                    var studentProfile = student.getStudentProfile();
                    return java.util.Map.of(
                        "name", student.getFullName(),
                        "email", student.getEmail(),
                        "department", studentProfile != null && studentProfile.getDepartment() != null ? studentProfile.getDepartment() : "N/A",
                        "enrolledDate", enrollment.getEnrolledAt().toString().split("T")[0]
                    );
                })
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", students);
    }
}
