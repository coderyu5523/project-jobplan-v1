package shop.mtcoding.projectjobplan.resume;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shop.mtcoding.projectjobplan._core.PagingUtil;
import shop.mtcoding.projectjobplan.board.BoardResponse;
import shop.mtcoding.projectjobplan.user.User;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ResumeController {
    private final ResumeRepository resumeRepository;
    private final HttpSession session;

    @PostMapping("resume/{id}/update")
    public String update(@PathVariable int id, ResumeRequest.UpdateDTO requestDTO){
        // todo 유효성 검사, 권한 검사
        resumeRepository.updateById(requestDTO, id);

        return "redirect:/user/" + id;
    }

    @PostMapping("/resume/{id}/upload")
    public String upload(@PathVariable int id, ResumeRequest.SaveDTO requestDTO){
        // todo 유효성 검사, 권한 검사
        resumeRepository.save(requestDTO, id);

        return "/user/" + id;
    }

    @GetMapping("/resume/main")
    public String main() {
        return "/resume/main";
    }
    @GetMapping("/resume/listings")
    public String listings(HttpServletRequest request, @RequestParam(defaultValue = "1")int page) {
        // 기업 메인 페이지
        List<ResumeResponse.ResumeAndUserDTO> responseDTO = resumeRepository.findByResumeAndUser(page);
        List<ResumeResponse.ResumeAndUserDTO> resumeList = new ArrayList<>();
        for (ResumeResponse.ResumeAndUserDTO dto : responseDTO) {
            if (dto.isEmployer()==false) {
                resumeList.add(dto);
            }
        }
        request.setAttribute("resumeList", resumeList);

        // 페이지네이션 모듈
        int totalPage = resumeRepository.countIsEmployerFalse();;
        PagingUtil paginationHelper = new PagingUtil(totalPage, page);

        request.setAttribute("nextPage", paginationHelper.getNextPage());
        request.setAttribute("prevPage", paginationHelper.getPrevPage());
        request.setAttribute("first", paginationHelper.isFirst());
        request.setAttribute("last", paginationHelper.isLast());
        request.setAttribute("numberList", paginationHelper.getNumberList());

        return "/resume/listings";
    }

    @GetMapping("/resume/{id}")
    public String detail(@PathVariable int id, HttpServletRequest request) {
        ResumeResponse.ResumeDetailDTO resumeDetailDTO = resumeRepository.detail(id);
        request.setAttribute("detail", resumeDetailDTO);

        return "/resume/detail";
    }

    @GetMapping("/resume/uploadForm")
    public String uploadForm() {
        return "/resume/uploadForm";
    }
    @GetMapping("/resume/{id}/updateForm")
    public String updateForm(@PathVariable int id, HttpServletRequest request) {
        Resume resume = resumeRepository.findById(id);
        request.setAttribute("resume", resume);

        return "/resume/updateForm";
    }

    @PostMapping("/resume/{id}/delete")
    public String delete(@PathVariable int id, HttpServletRequest request) {
        User user = (User) session.getAttribute("sessionUser");
        Resume resume = resumeRepository.findById(id);
        if (resume == null) {
            request.setAttribute("msg", "해당 아이디를 찾을 수 없습니다.");
            request.setAttribute("status", "404");
            return "/error";
        } else {
            resumeRepository.deleteById(id);
            return "redirect:/user/" + user.getId();
        }
    }
}
