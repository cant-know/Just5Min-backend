package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.dto.SendFriendRequestDTO;
import com.example.just5minbackend.service.FriendService;
import com.example.just5minbackend.vo.FriendRequestVO;
import com.example.just5minbackend.vo.FriendSearchVO;
import com.example.just5minbackend.vo.FriendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 好友：全部接口需登录（/api/friends 不在游客白名单里，未登录由 AuthInterceptor 直接 401）。
 * <p>注意路径顺序：{@code /requests/**} 与 {@code /search} 都写在 {@code /{friendId}} 之外，
 * 且 {@code /{friendId}} 只挂在 DELETE 上，因此不存在路由歧义。</p>
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 好友列表（含对方学习数据）。limit 默认 50、上限 50 */
    @GetMapping
    public Result<List<FriendVO>> list(@RequestParam(required = false) Integer limit,
                                       @RequestParam(required = false) Integer offset) {
        return Result.success(friendService.list(UserContext.require(), limit, offset));
    }

    /** 删除好友（双向） */
    @DeleteMapping("/{friendId}")
    public Result<Boolean> remove(@PathVariable Long friendId) {
        friendService.remove(UserContext.require(), friendId);
        return Result.success(true);
    }

    /** 搜索用户：用户ID / 手机号 精确，昵称 模糊 */
    @GetMapping("/search")
    public Result<List<FriendSearchVO>> search(@RequestParam(required = false) String keyword) {
        return Result.success(friendService.search(UserContext.require(), keyword));
    }

    /** 我收到的待处理好友请求 */
    @GetMapping("/requests")
    public Result<List<FriendRequestVO>> requests() {
        return Result.success(friendService.listIncoming(UserContext.require()));
    }

    /** 待处理请求数（tabBar 角标） */
    @GetMapping("/requests/count")
    public Result<Integer> requestCount() {
        return Result.success(friendService.pendingCount(UserContext.require()));
    }

    /** 发送好友请求 */
    @PostMapping("/requests")
    public Result<Boolean> send(@Valid @RequestBody SendFriendRequestDTO dto) {
        friendService.sendRequest(UserContext.require(), dto.getToUserId(), dto.getMessage());
        return Result.success(true);
    }

    /** 同意好友请求 */
    @PostMapping("/requests/{id}/accept")
    public Result<Boolean> accept(@PathVariable Long id) {
        friendService.accept(UserContext.require(), id);
        return Result.success(true);
    }

    /** 拒绝好友请求 */
    @PostMapping("/requests/{id}/reject")
    public Result<Boolean> reject(@PathVariable Long id) {
        friendService.reject(UserContext.require(), id);
        return Result.success(true);
    }
}
