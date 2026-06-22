# ================= Build stage =====================
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /app

# копируем csproj 
COPY Sole8_Backend/Sole8_Backend.csproj ./Sole8_Backend/
RUN dotnet restore ./Sole8_Backend/Sole8_Backend.csproj

# копируем исходники
COPY Sole8_Backend/. ./Sole8_Backend/
RUN dotnet publish ./Sole8_Backend/Sole8_Backend.csproj -c Release -o /out

# ================= Runtime stage =====================
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

RUN apt-get update && apt-get install -y libkrb5-3 && rm -rf /var/lib/apt/lists/*

COPY --from=build /out . 
# копируем весь wwwroot
COPY Sole8_Backend/wwwroot/. ./wwwroot/
# appsettings.json
COPY Sole8_Backend/appsettings.json ./appsettings.json

ENTRYPOINT ["dotnet", "Sole8_Backend.dll"]