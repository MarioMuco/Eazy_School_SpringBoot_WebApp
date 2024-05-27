package com.eazybytes.eazyschool.service;

import com.eazybytes.eazyschool.constants.EazySchoolConstants;
import com.eazybytes.eazyschool.model.Person;
import com.eazybytes.eazyschool.model.Roles;
import com.eazybytes.eazyschool.repository.PersonRepository;
import com.eazybytes.eazyschool.repository.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {

    private static final String UPLOAD_PATH = "src/main/resources/static/media/imgs";
    private final PersonRepository personRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonService(PersonRepository personRepository,
                         RolesRepository rolesRepository,
                         PasswordEncoder passwordEncoder) {

        this.personRepository = personRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean createNewPerson(Person person){

        boolean isSaved = false;
        Roles role = rolesRepository.getByRoleName(EazySchoolConstants.STUDENT_ROLE);
        person.setRoles(role);
        person.setPwd(passwordEncoder.encode(person.getPwd()));
        person = personRepository.save(person);
        if (null != person && person.getPersonId() > 0)
        {
            isSaved = true;
        }
        return isSaved;
    }

    public Person updatePerson(Person person, MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return personRepository.save(person);
        }

        Path uploadDir = Paths.get(UPLOAD_PATH + "/" + person.getPersonId());
        createOrCleanDirectory(uploadDir);

        String fileName = file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());

        person.setProfileImagePath(person.getPersonId() + "/" + fileName);
        return personRepository.save(person);
    }

    public List<Person> getAllLecturers() {

        List<Person> lecturers = new ArrayList<>();

        List<Person> persons = personRepository.findAll();
        for (Person person : persons) {
            if (person.getRoles().getRoleName().equals("LECTURER")) {
                lecturers.add(person);
            }
        }
        return lecturers;
    }

    public Person createNewLecturer(Person person) {

        Person personEntity = personRepository.readByEmail(person.getEmail());
        if (personEntity == null || !(personEntity.getPersonId() > 0)) {
            return null;
        }
        Roles role = rolesRepository.getByRoleName(EazySchoolConstants.LECTURER_ROLE);
        personEntity.setRoles(role);
        return personRepository.save(personEntity);
    }

    private void createOrCleanDirectory(Path directory) throws IOException {

        if (Files.notExists(directory)) {
            Files.createDirectories(directory);
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path path : stream) {
                    Files.delete(path);
                }
            }
        }
    }
}
