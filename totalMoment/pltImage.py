import matplotlib.pyplot as plt
import os

def totalMomentPLTImage(data, save_dir):
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)

    if len(data) == 3:
        array1 = data[0]
        array2 = data[1]
        array3 = data[2]

        # 绘制第一张图
        plt.figure()
        plt.plot(array1)
        plt.title('figure1')
        plt.xlabel('x')
        plt.ylabel('y')
        # 保存第一张图
        plt.savefig(os.path.join(save_dir, 'figure1.png'))
        plt.close()

        # 绘制第二张图
        plt.figure()
        plt.plot(array2)
        plt.title('figure2')
        plt.xlabel('x')
        plt.ylabel('y')
        # 保存第二张图
        plt.savefig(os.path.join(save_dir, 'figure2.png'))
        plt.close()

        # 绘制第三张图
        plt.figure()
        plt.plot(array3)
        plt.title('figure3')
        plt.xlabel('x')
        plt.ylabel('y')
        # 保存第三张图
        plt.savefig(os.path.join(save_dir, 'figure3.png'))
        plt.close()