import { http } from "@/utils/http";

export const getAsyncRoutes = () => {
  return http.get<any, HttpResult>("/router/getCurrentRouter");
};
