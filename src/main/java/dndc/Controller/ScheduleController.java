package dndc.Controller;

import com.amazonaws.services.xray.model.Http;
import dndc.Entity.Schedule;
import dndc.Service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @PostMapping("/ngo/complete_schedule/{scheduleID}")
    public ResponseEntity completeSchedule(@PathVariable String scheduleID) throws IOException {
        return new ResponseEntity(scheduleService.markComplete(scheduleID), HttpStatus.OK);
    }

    @PostMapping("/ngo/new_schedule")
    public ResponseEntity postSchedule(@RequestBody Schedule schedule) throws Exception {
        schedule.setScheduleTime(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        return new ResponseEntity(scheduleService.createSchedule(schedule), HttpStatus.CREATED);
    }


    @GetMapping("/ngo/my_schedule")
    public List<Schedule> mySchedule() throws Exception {
        //Hard code for search,  TODO delete
        return scheduleService.getMySchedule("89757");
    }


}
