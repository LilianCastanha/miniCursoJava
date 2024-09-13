package br.com.projetoPessoal.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetoPessoal.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskmodel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskmodel.setIdUser((UUID) idUser);

        var currentDAte = LocalDateTime.now();
        // 10/11/2023 - Current
        // 10/10/2023 - startAt

        if (currentDAte.isAfter(taskmodel.getStartAt()) || currentDAte.isAfter(taskmodel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio / data de término deve ser maior do que a data atual");
        }

        // Se a minha data de ínicio vier depois da data de término
        if (taskmodel.getStartAt().isAfter(taskmodel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio deve ser menor do que a data de término");
        }

        var task = this.taskRepository.save(taskmodel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var taskList = this.taskRepository.findByIdUser((UUID) idUser);
        return taskList;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskmodel, @PathVariable UUID id, HttpServletRequest request) {
        var task = this.taskRepository.findById(id).orElse(null);

        var idUser = request.getAttribute("idUser");

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");

        }

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar a tarefa");
        }

        Utils.copyNonNullProperties(taskmodel, task);
        var taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.ok().body(taskUpdated);
    }

}
