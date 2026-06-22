using System.Security.Claims;
using Microsoft.AspNetCore.Mvc;

namespace Sole8_Backend.Controllers;

public static class ControllerBaseExtensions
{
    public static int GetUserId(this ControllerBase controller)
    {
        // 1. Проверяем, авторизован ли пользователь в принципе
        if (controller.User?.Identity?.IsAuthenticated != true)
        {
            throw new UnauthorizedAccessException("Пользователь не авторизован.");
        }

        // 2. Ищем Claim с ID пользователя
        var claim = controller.User.FindFirst(ClaimTypes.NameIdentifier);
        
        // 3. Если Claim отсутствует или в нем не число — блокируем запрос, а не возвращаем 0
        if (claim == null || !int.TryParse(claim.Value, out int userId))
        {
            throw new UnauthorizedAccessException("Неверный или отсутствующий токен авторизации.");
        }

        return userId;
    }
}