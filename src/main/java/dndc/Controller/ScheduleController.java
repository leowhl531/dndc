package dndc.Controller;

import dndc.Entity.Schedule;
import dndc.Service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class ScheduleController {

    private ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService){
        this.scheduleService = scheduleService;
    }

    @PostMapping("/ngo/complete_schedule")
    public boolean completeSchedule(){
        //TODO
        return true;
    }

    @PostMapping("/ngo/new_schedule")
    public ResponseEntity postSchedule(@RequestBody Schedule schedule) throws Exception {
        schedule.setScheduleTime(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        return new ResponseEntity(scheduleService.createSchedule(schedule), HttpStatus.CREATED);
    }


    @GetMapping("/ngo/my_schedule")
    public List<Schedule> mySchedule(){
        //TODO
        return null;
    }


}
